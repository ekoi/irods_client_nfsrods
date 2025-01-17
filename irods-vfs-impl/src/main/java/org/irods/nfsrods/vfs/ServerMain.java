package org.irods.nfsrods.vfs;

import java.io.File;
import java.io.IOException;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.apache.logging.log4j.core.config.Configurator;
import org.dcache.nfs.ExportFile;
import org.dcache.nfs.v4.MDSOperationFactory;
import org.dcache.nfs.v4.NFSServerV41;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.oncrpc4j.rpc.OncRpcProgram;
import org.dcache.oncrpc4j.rpc.OncRpcSvc;
import org.dcache.oncrpc4j.rpc.OncRpcSvcBuilder;
import org.irods.jargon.core.connection.ClientServerNegotiationPolicy;
import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.nfsrods.config.NFSServerConfig;
import org.irods.nfsrods.config.ServerConfig;
import org.irods.nfsrods.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain
{
    // @formatter:off
    private static final String NFSRODS_CONFIG_HOME = System.getenv("NFSRODS_CONFIG_HOME");
    private static final String LOGGER_CONFIG_PATH  = NFSRODS_CONFIG_HOME + "/log4j.properties";
    private static final String SERVER_CONFIG_PATH  = NFSRODS_CONFIG_HOME + "/server.json";
    private static final String EXPORTS_CONFIG_PATH = NFSRODS_CONFIG_HOME + "/exports";
    // @formatter:on

    static
    {
        Configurator.initialize(null, LOGGER_CONFIG_PATH);
    }

    private static final Logger log_ = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) throws JargonException
    {
        log_.debug("START----");
        System.out.println("+++++++++++++ START ");
        ServerConfig config = null;

        try
        {
            config = JSONUtils.fromJSON(new File(SERVER_CONFIG_PATH), ServerConfig.class);
            log_.debug("main - Server config ==> {}", JSONUtils.toJSON(config));
        }
        catch (IOException e)
        {
            log_.error("main - Error reading server config." + System.lineSeparator() + e.getMessage());
            System.exit(1);
        }

        NFSServerConfig nfsSvrConfig = config.getNfsServerConfig();
        IRODSFileSystem ifsys = IRODSFileSystem.instance();
        OncRpcSvc nfsSvc = null;

        configureSslNegotiationPolicy(config, ifsys);

        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler<>(ifsys, "Closing iRODS connections")));

        try (CachingProvider cachingProvider = Caching.getCachingProvider();
             CacheManager cacheManager = cachingProvider.getCacheManager();)
        {
            IRODSAccessObjectFactory ifactory = ifsys.getIRODSAccessObjectFactory();
            IRODSIdMapper idMapper = new IRODSIdMapper(config, ifactory);

            // @formatter:off
            nfsSvc = new OncRpcSvcBuilder()
                .withPort(nfsSvrConfig.getPort())
                .withTCP()
                .withAutoPublish()
                .withWorkerThreadIoStrategy()
                .withSubjectPropagation()
                .build();

            Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler<>(nfsSvc, "Shutting down NFS services")));
            // @formatter:on

            ExportFile exportFile = new ExportFile(new File(EXPORTS_CONFIG_PATH));
            VirtualFileSystem vfs = new IRODSVirtualFileSystem(config, ifactory, idMapper, cacheManager);

            // @formatter:off
            NFSServerV41 nfs4 = new NFSServerV41.Builder()
                .withExportFile(exportFile)
                .withVfs(vfs)
                .withOperationFactory(new MDSOperationFactory())
                .build();
            // @formatter:on

            nfsSvc.register(new OncRpcProgram(100003, 4), nfs4);

            nfsSvc.start();

            log_.info("main - Press [ctrl-c] to shutdown.");

            Thread.currentThread().join();
        }
        catch (JargonException | IOException | InterruptedException e)
        {
            log_.error(e.getMessage());
        }
    }

    private static void configureSslNegotiationPolicy(ServerConfig _config, IRODSFileSystem _ifsys) throws JargonException
    {
        String policy = _config.getIRODSClientConfig().getSslNegotiationPolicy();
        SslNegotiationPolicy sslNegPolicy = ClientServerNegotiationPolicy.findSslNegotiationPolicyFromString(policy);
        log_.debug("configureClientServerNegotiationPolicy - Policy = {}", sslNegPolicy);

        IRODSSession session = _ifsys.getIrodsSession();
        SettableJargonProperties props = new SettableJargonProperties(session.getJargonProperties());
        props.setNegotiationPolicy(sslNegPolicy);
        session.setJargonProperties(props);
    }

    private static void close(Object _obj)
    {
        if (_obj == null)
        {
            return;
        }

        try
        {
            // @formatter:off
            if      (_obj instanceof OncRpcSvc)       { ((OncRpcSvc) _obj).stop(); }
            else if (_obj instanceof IRODSFileSystem) { ((IRODSFileSystem) _obj).closeAndEatExceptions(); }
            // @formatter:on
        }
        catch (Exception e)
        {
            log_.error(e.getMessage());
        }
    }

    private static final class ShutdownHandler<T> implements Runnable
    {
        private T object_;
        private String msg_;

        ShutdownHandler(T _object, String _msg)
        {
            object_ = _object;
            msg_ = _msg;
        }

        @Override
        public void run()
        {
            log_.info("main - {} ...", msg_);

            close(object_);

            log_.info("main - {} ... done.", msg_);
        }
    }
}
