#! /bin/sh

USAGE="Usage: $0 <Java keystore to sign with>"

if [ $# != 1 ]; then
   echo ${USAGE};
   exit 1;
elif [ ! -f "$1" ]; then
   echo "$1 doesn't seem to exist!";
   exit 1;
else
   jarsigner -keystore ../NEESit_keystore $1 NEESitCert;
   jarsigner -verify $1;
fi
