# Crison

# About

A data language for mining data out of websites and webservices (maybe others in the future).

# Requirements

* JVM Java 7
* phantomjs.binary.path set to 2.1.1 installation of phantomjs or later

# Usage

* Build a sample crison data file with a `.csn` extension
```
{:url! "http://www.somedomain.com"}
{:id "logo"}
{:tag :h2, :text "Home"}
{:click! "magicButton"}
```
* Place crison data file in a directory of your choice relative to this project
* Edit the value of PHANTOM_PATH in the run.sh file
* In the shell in the root of this project execute one of the following
  * `./run.sh input-dir output-dir`  (csn files are in input-dir)
  * `./run.sh input-dir`             (if no second param output-dir is input-dir)

# Examples

## Screenshots

Screenshots can be taken on any state changing directives:

```
{:url! "http://www.somedomain.com" :screenshot "arrived-on-homepage"}
{:id "logo"}
{:tag :h2, :text "Home"}
{:click! "magicButton" :screenshot "pressed-the-magic-button"}
```
