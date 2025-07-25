display_cmd=cat
which jq && display_cmd=jq # if jq is present in path use jq to format the json payload well

function execute(){
	cmd="curl -H 'x-tenant-id: t1' -H 'x-employee-id:E1' $1"
	header="curl -i $1"
	echo "Command: curl $1" | tr -d '\n' | tr '\t' ' ' | tr -s ' ' # all newlines ignored. all tabs to spaces. fuse multiple spaces to one space
	echo
	echo "---------------------------------------" 
	# eval $header 2> /dev/null | sed -n '/^[A-Za-z-]*:/p' # print only lines that have a word in the beginning (word can include -)
	echo 
	eval $cmd 2> /dev/null | $display_cmd
	echo "---------------------------------------" 
}

execute localhost:8080/info 

#----
json1='
  {
    "attribute1": "value-of-attribute1"
  }
'
tmpfile=/tmp/tmp.$$
echo "Creating serviceregistry with payload |$json1| "
curl -X POST -d "${json1}" -H 'Content-Type: application/json' -H 'x-chenile-eid: E1' -H 'x-chenile-tenant-id: t1' localhost:8080/serviceregistry > $tmpfile
cat $tmpfile | jq
id=$(cat $tmpfile | jq ".payload.id" | tr -d '"')
echo "Obtained ID = $id"
rm -f $tmpfile

# execute  "-X POST -d '${json_create}' -H 'Content-Type: application/json' localhost:8080/serviceregistry"

execute  "-X GET  -H 'Content-Type: application/json' localhost:8080/serviceregistry/${id}"

execute "-X GET -H 'Content-Type: application/json' localhost:8080/health-check/itemService"

execute "-X GET -H 'Content-Type: application/json' localhost:8080/service-info/itemService"
