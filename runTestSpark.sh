# https://stackoverflow.com/a/246128
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
if uname -s | grep -iq cygwin; then
  DIR=$(cygpath -w "$DIR")
  PWD=$(cygpath -w "$PWD")
fi
echo $DIR

echo "Provided arguments: $@"

if [ $# -ne "10" ]; then
  echo "$# arguments provided, expected 10 arguments in the following order:
      1) Path to the root directory of the project under test (ProjectPath)
      2) Path to the target file (.java file) (it MUST be relative to the ProjectPath)
      3) CUT qualified name (<package-name>.<class-name>)
      4) Classpaths containing the compiled project (seperated by ':')
      5) Model name (e.g., GPT-4)
      6) Grazie token
      7) txt-file containing prompt template
      8) Output directory
      9) Space username
      10) Space password"
  exit 1
fi


"$DIR/gradlew" -p "$DIR" headless -Proot="$1" -Pfile="$2" -Pcut="$3" -Pcp="$4" -Pllm="$5" -Ptoken="$6" -Pprompt="$7" -Pout="$8" -Dspace.username="$9" -Dspace.pass="${10}"