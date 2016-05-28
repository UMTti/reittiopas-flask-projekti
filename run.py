import os
from flask import Flask, url_for
from flask import render_template
app = Flask(__name__)

@app.route('/send_beacon_id/<String:beacon_id>')
def send_beacon_id(beacon_id):
    # show the post with the given id, the id is an integer
    return 'Post %d' % beacon_id


@app.route('/hello/')
def hello(name=None):
    return render_template('hello.html')

@app.route('/sivut/')
def default_page():
    return render_template('index_template.html')


if __name__ == "__main__":
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port = port)
