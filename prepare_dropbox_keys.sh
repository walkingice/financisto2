#!/bin/sh
sed -i -b s/"db-INSERT-APP-KEY-HERE"/"db-$1"/ ./Financisto/src/main/AndroidManifest.xml
sed -i -b s/"INSERT_APP_KEY_HERE"/"$1"/ ./Financisto/src/main/java/ru/orangesoftware/financisto2/export/dropbox/Dropbox.java
sed -i -b s/"INSERT_APP_SECRET_HERE"/"$2"/ ./Financisto/src/main/java/ru/orangesoftware/financisto2/export/dropbox/Dropbox.java
echo "Done, don't forget to revert the changes after building the release apk"