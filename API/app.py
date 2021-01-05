import csv
from datetime import datetime
from threading import Thread

from flask_sqlalchemy import SQLAlchemy
from flask import Flask, render_template, request, jsonify

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///forest-fire-alert.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = True
db = SQLAlchemy(app)


class Resident(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    phonenumber = db.Column(db.String(15), unique=True, nullable=False)
    forest = db.Column(db.String(50), nullable=False)


class Logs(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    timestamp = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    smoke = db.Column(db.String(120))
    sensor = db.Column(db.Integer, nullable=False)


class Sensor(db.Model):
    sensor = db.Column(db.Integer, primary_key=True)
    forest = db.Column(db.String(50), default="Mabira Forest")
    location = db.Column(db.String(20), nullable=False)


class Smslog(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)


class SMS(Thread):
    def __init__(self, phonenumbers, forest):
        super(SMS, self).__init__()
        self.phonenumbers = phonenumbers
        self.forest = forest

    def run(self):
        from twilio.rest import Client

        # the following line needs your Twilio Account SID and Auth Token
        client = Client("ACce2a6d98c15f8fe36be62bca6234a332", "ac0a6d764c58509740d86336a9cbe203")

        # change the "from_" number to your Twilio number and the "to" number
        # to the phone number you signed up for Twilio with, or upgrade your
        # account to send SMS to any phone number
        for phonenumber in self.phonenumbers:
            client.messages.create(to="+256"+phonenumber[1:],
                                   from_="+13043015338",
                                   body=f"Alert!! Fire has been detected in {self.forest}. Remain calm, Fire Fighters "
                                        f"are on the way")


@app.route("/", methods=["POST", "GET"])
def index():
    message = ""

    if request.method == "POST":

        phonenumber = request.form.get("phonenumber")
        forest = request.form.get("forest")
        batchFile = request.files["file"]

        if forest == "Select Forest You live near by" and not batchFile:
            print(not batchFile)
            message = "Select a forest"
            return render_template("index.html", message=message)

        if len(phonenumber) < 10 and not batchFile:
            message = f"{phonenumber} is Invalid"
            return render_template("index.html", message=message)

        if not batchFile:
            try:
                resident = Resident(phonenumber=phonenumber, forest=forest)
                db.session.add(resident)
                db.session.commit()
                message = f"{phonenumber} has been added Successfully!!"
            except:
                message = f"{phonenumber} has been added already b"

            return render_template("index.html", message=message)
        else:
            message="Contacts added Successfully!"
            decoded_file = batchFile.read().decode('utf-8').splitlines()
            csv_file = csv.DictReader(decoded_file)

            for line in csv_file:
                phonenumber = line['phonenumber']
                forest = line['forest']
                try:
                    resident = Resident(phonenumber=phonenumber, forest=forest)
                    db.session.add(resident)
                    db.session.commit()
                except:
                    message = f"{phonenumber} has been added already, remove it and try again"
            return render_template("index.html", message=message)

        return render_template("index.html", message=message)

    else:
        return render_template("index.html")


@app.route("/test")
def test():
    return "It works"


@app.route("/register_sensor")
def register():
    location = request.args.get("location")
    sensor = Sensor.query.get(1)
    if sensor:
        sensor.location = location
        db.session.commit()
    else:
        sensor = Sensor(forest="Mabira Forest", location=location)
        db.session.add(sensor)
        db.session.commit()
    return "Success"


@app.route("/log")
def log():
    smoke = request.args.get('smoke')
    location = request.args.get('location')

    if location:
        return register()

    log = Logs(smoke=smoke, sensor=1)
    db.session.add(log)
    db.session.commit()
    if int(smoke) >= 260:

        smslog = db.session.query(Smslog).order_by(Smslog.id.desc()).first()
        now = datetime.utcnow()
        if smslog is not None:
            duration = now - smslog.timestamp

        if smslog is None or int(duration.seconds) > 20:
            s = Sensor.query.get(log.sensor)
            residents = Resident.query.filter_by(forest=s.forest).all()
            phonenumbers = [p.phonenumber for p in residents]
            s = SMS(phonenumbers, s.forest)
            s.start()
            smslog = Smslog(timestamp=now)
            db.session.add(smslog)
            db.session.commit()
    return "Success"


@app.route("/fire_status")
def fire():
    log = db.session.query(Logs).order_by(Logs.id.desc()).first()
    smoke = log.smoke

    s = db.session.query(Sensor).get(log.sensor)

    location = s.location
    r = {"forest": s.forest, "sensor": s.sensor}

    status = 0
    if int(smoke) >= 260:
        status = 1

    r["fire"] = status
    r["location"] = location
    return jsonify(r)


@app.route("/offline_status")
def offline():
    log = db.session.query(Logs).order_by(Logs.id.desc()).first()
    duration = datetime.utcnow() - log.timestamp

    s = Sensor.query.get(log.sensor)
    location = s.location
    r = {"forest": s.forest, "sensor": s.sensor}

    status = 1
    if int(duration.seconds) > 7:
        status = 0
    r["online"] = status
    r["location"] = location

    return jsonify(r)


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0")
