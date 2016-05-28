from flask import Flask, url_for, render_template, request
import os
import psycopg2

app = Flask(__name__)


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
    return stop_id


@app.route('/stop/<beacon_id>')
def send_beacon_id(beacon_id):
    beacon_ids = {'2f2f': 'HSL:1362141'}
    buses = ""
    if beacon_id in beacon_ids:
        buses = search_buses_by_stop_id(beacon_ids[beacon_id])

    return buses


@app.route('/sivut/')
def default_page():
    return render_template('index_template.html')


if __name__ == "__main__":
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port = port)
