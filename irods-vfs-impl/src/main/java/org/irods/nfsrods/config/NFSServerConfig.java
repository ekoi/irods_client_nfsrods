package org.irods.nfsrods.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NFSServerConfig
{
    private int port_;
    private String iRODSMntPoint_;
    private int userInfoRefreshTimeInMillis_;
    private int fileInfoRefreshTimeInMillis_;
    private int userAccessRefreshTimeInMillis_;
    
    // @formatter:off
    @JsonCreator
    NFSServerConfig(@JsonProperty("port")                                          Integer _port,
                    @JsonProperty("irods_mount_point")                             String _iRODSMountPoint,
                    @JsonProperty("user_information_refresh_time_in_milliseconds") Integer _userInfoRefreshTimeInMillis,
                    @JsonProperty("file_information_refresh_time_in_milliseconds") Integer _fileInfoRefreshTimeInMillis,
                    @JsonProperty("user_access_refresh_time_in_milliseconds")      Integer _userAccessRefreshTimeInMillis)
    {
        ConfigUtils.throwIfNull(_port, "port");
        ConfigUtils.throwIfNull(_iRODSMountPoint, "irods_mount_point");
        ConfigUtils.throwIfNull(_userInfoRefreshTimeInMillis, "user_information_refresh_time_in_milliseconds");
        ConfigUtils.throwIfNull(_fileInfoRefreshTimeInMillis, "file_information_refresh_time_in_milliseconds");
        ConfigUtils.throwIfNull(_userAccessRefreshTimeInMillis, "user_access_refresh_time_in_milliseconds");

        port_ = _port;
        iRODSMntPoint_ = _iRODSMountPoint;
        userInfoRefreshTimeInMillis_ = _userInfoRefreshTimeInMillis;
        fileInfoRefreshTimeInMillis_ = _fileInfoRefreshTimeInMillis;
        userAccessRefreshTimeInMillis_ = _userAccessRefreshTimeInMillis;
    }
    // @formatter:on

    @JsonIgnore
    public int getPort()
    {
        return port_;
    }

    @JsonIgnore
    public String getIRODSMountPoint()
    {
        return iRODSMntPoint_;
    }
    
    @JsonIgnore
    public int getUserInfoRefreshTimeInMilliseconds()
    {
        return userInfoRefreshTimeInMillis_;
    }
    
    @JsonIgnore
    public int getFileInfoRefreshTimeInMilliseconds()
    {
        return fileInfoRefreshTimeInMillis_;
    }

    @JsonIgnore
    public int getUserAccessRefreshTimeInMilliseconds()
    {
        return userAccessRefreshTimeInMillis_;
    }
}
