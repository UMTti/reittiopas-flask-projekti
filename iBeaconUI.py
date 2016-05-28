    
from flask import Flask, url_for, render_template, request
import os
import blescan
import sys
import requests
import logging
from logging.handlers import RotatingFileHandler
import json

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

    returnedList = blescan.parse_events(sock, 100)
    app.logger.info(returnedList)
    print "----------"
    for beacon in returnedList:
        if '2f234454cf6d4a0fadf2f4911ba9ffa6' in beacon:
        	#2f234454cf6d4a0fadf2f4911ba9ffa6
            app.logger.info("beacon loydetty")
            r = requests.get("http://stop2.herokuapp.com/stop/2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
            content = r.content
            content = json.loads(content)
        else:
            content = "Et ole pysakin tai bussin lahistolla"
            app.logger.info("beacon EI loydetty")
    return render_template('index_templatelocal.html', content=content)



if __name__ == "__main__":
    port = int(os.environ.get('PORT', 5000))
    handler = RotatingFileHandler('foo.log', maxBytes=10000, backupCount=1)
    handler.setLevel(logging.INFO)
    app.logger.addHandler(handler)
    app.run(host='0.0.0.0', port = port)


