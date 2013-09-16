# zoodump

Zoodump is a command line application for uploading and downloading
data from zookeeper in a very simple way.

IF HANDLES DATA IN A VERY SPECIFIC FORMAT, SO IT IS NOT SUITABLE FOR EVERYONE.

In particular it ignores information inside entries that have kids, effectively
treating them as directories.

Data is downloaded an uploaded as edn.

## Installation

Clone the repository and build with lein uberjar

## Usage

Very simple.. sample usage:

    $ java -jar zoodump-0.1.0-standalone.jar --url localhost:2181 --action import --file data.edn --base /apoya

For exporting: 

    $ java -jar zoodump-0.1.0-standalone.jar --url localhost:2181 --action export --file data.edn --base /apoya

:)
