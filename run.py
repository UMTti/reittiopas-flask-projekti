
from flask import Flask, url_for
from flask import render_template
app = Flask(__name__)


@app.route('/hello/')
def hello(name=None):
    return render_template('hello.html')

@app.route('/sivut/')
def default_page():
    return render_template('index_template.html')


if __name__ == "__main__":
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port = port)
