FROM ubuntu:16.04

RUN apt-get update && \
    apt-get install -y apt-transport-https curl telnet maven git openjdk-8-jdk \
                       ninja-build less vim wget lsb-release gcc g++ python

RUN wget -qO - https://packages.irods.org/irods-signing-key.asc | apt-key add -; \
    echo "deb [arch=amd64] https://packages.irods.org/apt/ $(lsb_release -sc) main" | tee /etc/apt/sources.list.d/renci-irods.list; \
    apt-get update && \
    apt-get install -y irods-dev irods-externals-cmake3.11.4-0

ENV PATH=/opt/irods-externals/cmake3.11.4-0/bin:$PATH
#ENV NFSRODS_CONFIG_HOME=/nfsrods_config

ARG _github_account="ekoi"
ARG _sha="master"

RUN git clone https://github.com/${_github_account}/irods_client_nfsrods

#RUN cd irods_client_nfsrods && \
#    git checkout ${_sha} && \
#    mvn clean install -Dmaven.test.skip=true

RUN mkdir _package && cd _package && \
    cmake -GNinja /irods_client_nfsrods && \
    cpack -G "DEB" && \
    dpkg -i irods*.deb
ADD irods-vfs-impl/config nfsrods_config
ADD start.sh /
RUN chmod +x start.sh

#ENTRYPOINT ["./start.sh"]
CMD ["tail", "-f", "/dev/null"]
