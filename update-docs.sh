#! /bin/bash

bucketURL="gs://entur-docs.appspot.com/mobility"

echo "Updating documentation at $bucketURL"
rsync -R **/*.mdx temp-docs && gsutil -m rsync -r temp-docs $bucketURL && rm -rf temp-docs
echo "Done!"