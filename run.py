
from flask import Flask, url_for, render_template
import os
import psycopg2


app = Flask(__name__)

class Model:

    def __init__(self, dbname, port):
        self.conn = psycopg2.connect("dbname=%s port=%d" % (dbname, port))
        self.cur = self.conn.cursor()
        psycopg2.extensions.register_type(psycopg2.extensions.UNICODE, self.cur)

@app.route('/hello/')
def hello(name=None):
    return render_template('hello.html')


@app.route('/sivut/')
def default_page():
    return render_template('index_template.html')


if __name__ == "__main__":
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port = port)
