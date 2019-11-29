FROM python:2.7
#FROM ubuntu:18.04

MAINTAINER Tariq M Nasim "tnasim@asu.edu"

USER root

RUN apt-get update -y \
	&& apt-get install -y python-pip python-dev python-flask nano curl \
	&& cd /usr/local/bin \
	&& pip install --upgrade pip
	# && ln -s /usr/bin/python python \

# Copy the requirements.txt
COPY ./requirements.txt /app/requirements.txt

WORKDIR /app

RUN pip install -r requirements.txt

# Copy all other files from the current directory into the working directory.
COPY . /app

#ENTRYPOINT [ "/bin/bash" ]

#ENTRYPOINT [ "python" ]

#CMD [ "app.py" ]
