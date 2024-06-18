#!/usr/bin/env bash

# https://stackoverflow.com/a/246128
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
if uname -s | grep -iq cygwin; then
  DIR=$(cygpath -w "$DIR")
  PWD=$(cygpath -w "$PWD")
fi
echo $DIR

echo "Provided arguments are $@"

if [ $# -ne "11" ]; then
  echo "$# arguments provided, expected 11 arguments in the following order:
        1) Path to the root directory of the project under test (ProjectPath)
        2) Path to the target file (.java file) (it MUST be relative to the ProjectPath)
        3) Qualified name of the class under test (i.e., <package-name>.<class-name>)
        4) Classpaths containing the compiled project (separated by ':')
        5) Version of JUnit testing framework (either 4 or 5)
        6) Model name (e.g., GPT-4)
        7) Grazie token
        8) Filepath to a txt-file containing prompt template
        9) Output directory
        10) Space username
        11) Space password"
  exit 1
fi

echo -Proot="$1" -Pfile="$2" -Pcut="$3" -Pcp="$4" -Pjunitv="$5" -Pllm="$6" -Ptoken="$7" -Pprompt="$8" -Pout="$9" -Dspace.username="${10}" -Dspace.pass="${11}"
"$DIR/gradlew" -p "$DIR" headless -Proot="$1" -Pfile="$2" -Pcut="$3" -Pcp="$4" -Pjunitv="$5" -Pllm="$6" -Ptoken="$7" -Pprompt="$8" -Pout="$9" -Dspace.username="${10}" -Dspace.pass="${11}"
