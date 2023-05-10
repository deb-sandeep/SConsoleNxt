# SConsole server system operations
<!-- ----------------------------------------------------------------------- -->

1. Setting up JAVA_HOME
2. Setting up variables at a global level (DB_HOST,...)
3. Home folder structure
4. Installing sconsole as a systemd service
5. Listing all serivices

## 1. Setting up JAVA_HOME
<!-- ----------------- -->

* Add ~/.bash_vars extension to ~/.bashrc at the end of file.
```
# All the user defined bash variables are in the .bash_vars file
if [ -f ~/.bash_vars ]; then
    . ~/.bash_vars
fi
```

* Create ~/.bash_vars if required
* Edit ~/.bash_vars to set JAVA_HOME
```
export JAVA_HOME=/usr/lib/jvm/java-1.17.0-openjdk-arm64
```

=> Source ~/.bashrc

## 2. Setting up variables at a global level
<!-- ------------------------------------- -->

* Edit environment.sh in /etc/profile.d/ folder
* Content of environment.sh
```
#!/bin/bash

export DB_PASSWORD=<password>
export DB_HOST=<hostname>
export WORDNIC_API_KEY=<api_key>
```
* source /etc/profile.d/environment.sh

## 3. Home folder structure
<!-- ------------------ -->

```
Desktop
projects
   +- bin
      +- sconsole -> sconsole-0.1-SNAPSHOT/
      +- sconsole-0.1-SNAPSHOT   
   +- workspace
      +- sconsole
        +- log
```

## 4. Installing sconsole as a systemd service
<!-- ------------------------------------- -->

* sudo vi /lib/systemd/system/sconsole.service
* Copy the contents of local file doc/sysops/sconsole.service to vi
* Change the value of Environment variables value in the service. Save
* Test if the service works `sudo systemctl start sconsole.service`
* Stop the service `sudo systemctl stop sconsole.service`
* Enable the service `sudo systemctl enable sconsole.service`
* Reboot the system and see if sconsole comes up at boot

## 5. Listing all services
<!-- ------------------------------------- -->

systemctl list-units
