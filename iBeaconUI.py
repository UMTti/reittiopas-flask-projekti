    
from flask import Flask, url_for, render_template, request
import os
import blescan
import sys
import requests
import logging
from logging.handlers import RotatingFileHandler
import json
from datetime import datetime

import bluetooth._bluetooth as bluez


app = Flask(__name__)


@app.route('/sivut/')
def default_page():
    dev_id = 0
    try:
        sock = bluez.hci_open_dev(dev_id)
        app.logger.info("ble thread started")
    except:
        app.logger.info("error accessing bluetooth device...")
        sys.exit(1)

    blescan.hci_le_set_scan_parameters(sock)
    blescan.hci_enable_le_scan(sock)

    returnedList = blescan.parse_events(sock, 10)
    app.logger.info(returnedList)
    print "----------"
    setti = set()
    stop_name = ""
    for beacon in returnedList:
        if '2f234454cf6d4a0fadf2f4911ba9ffa6' in beacon:
            app.logger.info("beacon loydetty")
            r = requests.get("http://stop2.herokuapp.com/stop/2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
            content = r.content
            content = json.loads(content)
            stop_name = content['stop_name']
            palautus = "<h3>Press button to stop bus:</h3> "
            for asd in content['schedule']:
                setti.add(asd['line'])
                arrival = datetime.fromtimestamp(int(asd['arrival'])).strftime('%H:%M')
                palautus += " <div class='btn btn-lg stop_bus' style='margin:5px;color:white;background:#F092CD;' id='" + asd['line'] + "'>" +  asd['line'] + " " + arrival \
                            + "</div>  "
            content = palautus
            break
        else:
            content = "<h3>You're not near stop</h3>"
            app.logger.info("beacon EI loydetty")
    return render_template('index_templatelocal.html', content=content, setti=setti, stop_name=stop_name)


@app.route('/stops')
def show_stops():
    stops = '''
            {"name": "718 to Rautatientori (HSL:1020201)", "stops": [
            {"code": "3032", "name": "Valtimontie", "gtfsId": "HSL:1240123"},
            {"code": "3030", "name": "Sumatrantie", "gtfsId": "HSL:1240106"},
            {"code": "3028", "name": "Kumpulan kampus", "gtfsId": "HSL:1240118"},
            {"code": "3024", "name": "Vallilan varikko", "gtfsId": "HSL:1220104"},
            {"code": "3022", "name": "Ristikkokatu", "gtfsId": "HSL:1220102"},
            {"code": "2410", "name": "S\u00f6rn\u00e4inen(M)", "gtfsId": "HSL:1113131"},
            {"code": "2404", "name": "Haapaniemi", "gtfsId": "HSL:1112126"},
            {"code": "2402", "name": "Hakaniemi", "gtfsId": "HSL:1111114"},
            {"code": null, "name": "Rautatientori", "gtfsId": "HSL:1020201"}]}
            '''
    return render_template('show_stops.html', stops=json.loads(stops))



if __name__ == "__main__":
    port = int(os.environ.get('PORT', 5050))
    handler = RotatingFileHandler('foo.log', maxBytes=10000, backupCount=1)
    handler.setLevel(logging.INFO)
    app.logger.addHandler(handler)
    app.run(host='0.0.0.0', port = port)


