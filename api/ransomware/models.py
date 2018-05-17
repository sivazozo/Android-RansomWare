from pony.orm.core import Database, PrimaryKey, Required, Optional
import uuid

db = Database()

class Mobile(db.Entity):
    uuid = PrimaryKey(uuid.UUID, default=uuid.uuid4)
    imei = Required(str)
    os_version = Required(str)
    device_version = Required(str)
    device_token = Required(str)
    public_key = Required(str)
    private_key = Required(str)
    ip = Required(str)


    def output(self):
        return {
            "uuid": self.uuid,
            "imei": self.imei,
            "os_version": self.os_version,
            "device_version": self.device_version,
            "device_token": self.device_token,
            "public_key": self.public_key,
            "private_key": self.private_key,
            "ip": self.ip
        }