    
from flask import Flask, url_for, render_template, request
import os
import blescan
import sys
import requests

import bluetooth._bluetooth as bluez


app = Flask(__name__)


@app.route('/sivut/')
def default_page():
    returnedList = blescan.parse_events(sock, 10)
    dev_id = 0
    try:
        sock = bluez.hci_open_dev(dev_id)
        print "ble thread started"

    except:
        print "error accessing bluetooth device..."
        sys.exit(1)

    blescan.hci_le_set_scan_parameters(sock)
    blescan.hci_enable_le_scan(sock)

    while True:
	    returnedList = blescan.parse_events(sock, 10)
	    print "----------"
        for beacon in returnedList:
            if '2f234454cf6d4a0fadf2f4911ba9ffa6' in beacon:
                r = requests.get("http:///stop2.herokuapp.com/stop/2f234454cf6d4a0fadf2f4911ba9ffa6")
                content = r.content
    return render_template('index_templatelocal.html', content=content)



if __name__ == "__main__":
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port = port)


