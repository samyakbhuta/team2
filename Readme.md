ErrorBuster
-----------

ErrorBuster is Google Glass application that helps you ...
* capture the error displayed on screen ( or somewhere else )
* extract the text from the snapshot
* identify error(s) in the extracted texts
* Map errors to possible solutions ( By means of Google or with StackOverflow etc)


### Capturing Error

Standard Google Glass functionality.

### Text extraction

[Tesseract](http://code.google.com/p/tesseract-ocr/) to OCR the text from the captured image.

### Identifying Errors

We plan to use [Google Prediction API](https://developers.google.com/prediction/) to identify errors.

### Mapping errors

As of now, errors will be fed to search engine. Later, Stack Overflow or other such services can be used for more accurate mapping.Google Glass Hackathon
