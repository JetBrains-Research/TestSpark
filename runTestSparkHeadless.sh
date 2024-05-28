# https://stackoverflow.com/a/246128
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
if uname -s | grep -iq cygwin; then
  DIR=$(cygpath -w "$DIR")
  PWD=$(cygpath -w "$PWD")
fi
echo $DIR

echo "Provided arguments are $@"

if [ $# -ne "11" ]; then
  echo "$# arguments provided"
  echo "needs 10 arguments with the following order: 1- project under test root path 2- CUT-File-directory, 3-CUT qualified name, 4-Classpaths containing the compiled project, 5- JUnitVersion ('4' or '5'), 6-Model name, 7-Grazie toke 8-.txt file containing prompt template 9- output directory 10-Space username 11-Space password"
  exit 1
fi


"$DIR/gradlew" -p "$DIR" headless -Proot="$1" -Pfile="$2" -Pcut="$3" -Pcp="$4" -Pjunitv="$5" -Pllm="$6" -Ptoken="$7" -Pprompt="$8" -Pout="$9" -Dspace.username="$10" -Dspace.pass="${11}"