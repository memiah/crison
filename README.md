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

## Pause after changing the state of the app

It is possible to configure a delay before screenshotting or moving on in the
app after a click or a form submission:
```
{:fill-submit!
 [{:text! "setThisText" :tag :input :placeholder "Enter your text"}
  {:value "Search" :class "page-submit"}]
 :pause 2000 :screenshot "my-page" :source "my-page-search"}
```
A default of 1 second is set on all 'directives'; :url!, :click! etc.
The value is an Integer in milliseconds.

## Screenshots

Screenshots can be taken on any state changing directives:

```
{:url! "http://www.somedomain.com" :screenshot "arrived-on-homepage"}
{:id "logo"}
{:tag :h2, :text "Home"}
{:click! "magicButton" :screenshot "pressed-the-magic-button"}
```

Simple forms with decent structural markup and have multi fields filled:
```
{:fill!
 [{"field-1" "Thatcham"}
  {"field-2" "Camberley"}]
 :screenshot "fill-form"}
```

Fields can be cleared first:
```
{:fill!
 [{"field-1" clear}
  {"field-1" "Thatcham"}
  {"field-2" clear}
  {"field-2" "Camberley"}]
 :screenshot "search"}
```

Where structural markup is not so good on forms we can select over a sequence of
attributes to try to guarantee uniqueness.  Oh and we can combine filling with
submitting (the last item in the array indicates the submit element):
```
{:fill-submit!
 [{:text! "MySearchTerm1" :tag :input :placeholder "Enter your searchterm 1"}
  {:text! "MySearchTerm2" :tag :input :placeholder "Enter your searchterm 2"}
  {:value "Search" :class "mypage-search-submit"}]
 :screenshot "main-search"}
```
