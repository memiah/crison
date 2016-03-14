# Crison

# About

A data language for mining data out of websites and webservices (maybe others in the future).

# Requirements

* JVM Java 7
* phantomjs.binary.path set to 2.1.1 installation of phantomjs or later

# Usage

* Build a sample crison data file
```
{:url! "http://www.somedomain.com"}
{:id "logo"}
{:tag :h2, :text "Home"}
{:click! "magicButton"}
```
* Edit the value of PHANTOM_PATH in the run.sh file
* Execute `./run.sh` in your shell in the root directory of this installation
