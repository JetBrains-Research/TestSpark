# https://stackoverflow.com/a/246128
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
if uname -s | grep -iq cygwin; then
  DIR=$(cygpath -w "$DIR")
  PWD=$(cygpath -w "$PWD")
fi
echo $DIR

echo "Provided arguments are $@"

if [ $# -ne "10" ]; then
  echo "$# arguments provided"
  echo "needs 10 arguments with the following order: 1- project under test root path 2- CUT-File-directory, 3-CUT qualified name, 4-Classpaths containing the compiled project, 5-Model name, 6-Grazie toke 7-.txt file containing prompt template 8- output directory 9-Space username 10-Space password"
  exit 1
fi


"$DIR/gradlew" -p "$DIR" headless -Proot="$1" -Pfile="$2" -Pcut="$3" -Pcp="$4" -Pllm="$5" -Ptoken="$6" -Pprompt="$7" -Pout="$8" -Dspace.username="$9" -Dspace.pass="${10}"