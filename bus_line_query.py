import requests
import json

class BusStops:
    def __init__(self, bus_id='HSL:4718', direction=1):
        self.url = "http://api.digitransit.fi/routing/v1/routers/hsl/index/graphql"
        self.headers = {'Content-Type': 'application/graphql'}
        self.query = ("{"
                      "  pattern(id:\"%s:%s:01\") {"
                      "    name"
                      "    stops{"
                      "      name"
                      "      code"
                      "      gtfsId"
                      "    }"
                      "  }"
                      "}") % (bus_id, direction)

    def bus_stops(self):
        r = requests.post(self.url, data=self.query, headers=self.headers)
        data = json.loads(r.text)["data"]["pattern"]
        route = {'name': data["name"], 'stops': []}
        for stop in data["stops"]:
            route['stops'].append(stop)

        return route
