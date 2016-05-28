import requests
import json
import datetime

stop_id = "HSL:1362141"
date = "20160528"

url = "http://api.digitransit.fi/routing/v1/routers/hsl/index/graphql"
headers = {'Content-Type': 'application/graphql'}

q = ("{stop(id: \"%s\") {"
     "  stoptimesForServiceDate(date: \"%s\"){"
     "    pattern {"
     "      id"
     "      name"
     "      route {"
     "        gtfsId"
     "        shortName"
     "        longName"
     "      }"
     "    }"
     "      stoptimes {"
     "        serviceDay"
     "      	scheduledArrival"
     "    	realtimeArrival"
     "      }"
     "    }"
     "  }"
     "}") % (stop_id, date)

r = requests.post(url, data=q, headers=headers)
data = json.loads(r.text)

lines = data["data"]["stop"]["stoptimesForServiceDate"]

for line in lines:
    print(line["pattern"]["route"]["shortName"])
    stoptimes = line["stoptimes"]
    for time in stoptimes:
        arrival = datetime.datetime.fromtimestamp(time["serviceDay"] + time["realtimeArrival"]).strftime('%H:%M')
        print(arrival)
