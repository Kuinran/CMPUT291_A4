#!/usr/bin/perl

# This takes inuput lines of the form <key,data> and break the lines at the 
# first comma; this can be used to prepare the data for db_load
while (<STDIN>) {
  chomp;
  if (/^([^,]+):(.*?)$/) {
    $key=$1; $rec=$2;
    # BDB treats backslash as a special character, and we would get rid of it!
    $rec =~ s/\\/&92;/g;
    print $key, "\n", $rec, "\n";
  }
}
