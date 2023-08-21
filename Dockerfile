#FROM openjdk:8-jdk-alpine
FROM openjdk:8

# 安装Python和依赖项
#WORKDIR /app


#RUN apt-get update
#RUN apt-get install -y tzdata
#RUN apt-get install inetutils-ping -y
#RUN apt-get update
#RUN apt install -y python3-pip
#RUN pip3 install --upgrade pip -i https://pypi.tuna.tsinghua.edu.cn/simple
#RUN pip3 config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple
#RUN pip3 config set global.extra-index-url "http://mirrors.aliyun.com/pypi/simple/ http://pypi.mirrors.ustc.edu.cn/simple/ http://pypi.hustunique.com/ http://pypi.douban.com/simple/ http://mirrors.cloud.tencent.com/pypi/simple https://repo.huaweicloud.com/repository/pypi/simple/"
#RUN pip3 install xgboost==1.7.4
#RUN pip install numpy==1.22.4
#RUN pip3 install scikit-learn==1.3.0
#RUN pip3 install hyperopt==0.2.7
#RUN pip3 install pandas==1.5.3
#RUN apt-get update
#RUN #apt-get install -y openjdk-8-jdk
#RUN pip3 install shap==0.42.1
RUN apt-get update && \
    apt-get install -y tzdata inetutils-ping python3-pip && \
    pip3 install --upgrade pip -i https://pypi.tuna.tsinghua.edu.cn/simple && \
    pip3 config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple && \
#    pip3 config set global.extra-index-url "http://mirrors.aliyun.com/pypi/simple/ http://pypi.mirrors.ustc.edu.cn/simple/ http://pypi.hustunique.com/ http://pypi.douban.com/simple/ http://mirrors.cloud.tencent.com/pypi/simple https://repo.huaweicloud.com/repository/pypi/simple/" && \
    pip3 install xgboost==1.7.4 numpy==1.22.4 scikit-learn==1.3.0 hyperopt==0.2.7 pandas==1.5.3 shap==0.42.1 --default-timeout=1688 && \
    apt remove -y --auto-remove curl make gcc &&\
    apt-get clean && \
    rm  -rf /var/lib/apt/lists/*

#WORKDIR /app
COPY target /app/
ENV TZ=Asia/Shanghai

# 将下载的jdk 的压缩包拷贝到镜像中，注意 ADD和COPY的区别，ADD 会解压，COPY不会解压
#COPY jdk1.8.0_202 /usr/local/jdk

# 设置JAVA_HOME 的环境变量
#ENV JAVA_HOME /usr/local/jdk/jdk1.8.0_202

# 设置JAVA 环境
#ENV CLASSPATH=$JAVA_HOME/bin:$JAVA_HOME/lib:$JAVA_HOME/jre/lib

# 将java可执行文件设置到PATH中，这样就可以使用java命令了
#ENV PATH=.:$JAVA_HOME/bin:$JAVA_HOME/jre/bin:$PATH


ENTRYPOINT ["nohup","java", "-jar", "./app/RiskPredict-0.0.1-SNAPSHOT.jar","-Dfile.encoding=utf-8","--server.port=8082"]
#ENTRYPOINT ["java", "-jar", "./app/RiskPredict-0.0.1-SNAPSHOT.jar","--server.port=8082"]
