import requests
import json
import datetime


class StopSchedule:
    def __init__(self, stop_id="HSL:1362141", date="20160528"):
        self.url = "http://api.digitransit.fi/routing/v1/routers/hsl/index/graphql"
        self.headers = {'Content-Type': 'application/graphql'}

        self.query = ("{stop(id: \"%s\") {"
             "  name"
             "  code"
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

    def schedule(self):
        r = requests.post(self.url, data=self.query, headers=self.headers)
        data = json.loads(r.text)["data"]["stop"]

        lines = data["stoptimesForServiceDate"]

        current_time = datetime.datetime.now()

        sched = []
        for line in lines:
            name = (line["pattern"]["route"]["shortName"])
            stoptimes = line["stoptimes"]
            for time in stoptimes:
                arrival = datetime.datetime.fromtimestamp(time["serviceDay"] + time["realtimeArrival"])
                if current_time < arrival:
                    sched.append({'line': name, 'arrival': arrival.strftime("%s")})

        sorted_list = sorted(sched, key=lambda k: k['arrival'])
        sorted_list.insert(0, {'stop_name': data["name"], 'stop_code': data["code"]})
        return sorted_list
