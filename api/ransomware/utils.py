import base64

from Cryptodome.PublicKey import RSA

config = {
    "format": "DER",
    "pkcs": 8
}


def generate_rsa():
    key = RSA.generate(2048)
    private_key = key.export_key(**config)
    public_key = key.publickey().export_key(**config)
    public_key = base64.b64encode(public_key).decode("utf-8")
    private_key = base64.b64encode(private_key).decode("utf-8")

    return public_key, private_key
