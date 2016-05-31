from flask import Flask, url_for, render_template, request, session, redirect, escape
import os
import psycopg2
from stop_schedule_query import StopSchedule
from bus_line_query import BusStops
import json
from flask_socketio import SocketIO, emit

app = Flask(__name__)

app.secret_key = "supersecretkey"
socketio = SocketIO(app)

class Model:

    def __init__(self, dbname, port):
        self.conn = psycopg2.connect("dbname=%s port=%d" % (dbname, port))
        self.cur = self.conn.cursor()
        psycopg2.extensions.register_type(psycopg2.extensions.UNICODE, self.cur)

    def sql(self, query, args=()):
        self.cur.execute(query, args)
        return self.cur.fetchall()

    def commit(self):
        self.conn.commit()


@app.route('/hello/')
def hello(name=None):
    return render_template('hello.html')


def search_buses_by_stop_id(stop_id):
    s = StopSchedule()
    j = json.dumps(s.schedule())
    return j


@app.route('/stop/<beacon_id>')
def send_beacon_id(beacon_id):
    beacon_ids = {'2f234454-cf6d-4a0f-adf2-f4911ba9ffa6': 'HSL:1362141'}
    buses = ""
    if beacon_id in beacon_ids:
        buses = search_buses_by_stop_id(beacon_ids[beacon_id])

    return buses


@app.route('/bus/<bus_id>/<direction>')
def bus_stops(bus_id, direction):
    bus = BusStops(bus_id, direction)
    route = bus.bus_stops()
    return json.dumps(route)


@app.route('/sivut/')
def default_page():
    return render_template('index_template.html')


@app.route('/driver')
def driver_page():
    if 'username' in session:
        bus = BusStops()
        stops = bus.bus_stops()
        return render_template('driver_template.html', stops=stops)
    else:
        return 'Not logged in'


@app.route('/driver/login', methods=['GET', 'POST'])
def driver_login():
    if request.method == 'POST':
        session['username'] = request.form['username']
        return redirect(url_for('driver_page'))
    return '''
            <form action="" method="post">
                <p><input type=text name=username>
                <p><input type=submit value=Login>
            </form>
            '''

@app.route('/testuser')
def user():
    return render_template('dummy_client.html')


@socketio.on('my event', namespace='/driver')
def test_message(message):
    emit('my response', {'data': message['data']})


@socketio.on('bus selected event', namespace='/driver')
def test_message(message):
    emit('stop bus', {'data': message['data']}, broadcast=True)


@socketio.on('connect', namespace='/driver')
def test_connect():
    emit('my response', {'data': 'Connected'})


@socketio.on('disconnect', namespace='/driver')
def test_disconnect():
    print('Client disconnected')


if __name__ == "__main__":
    port = int(os.environ.get('PORT', 5000))
    socketio.run(app, host='0.0.0.0', port = port)
