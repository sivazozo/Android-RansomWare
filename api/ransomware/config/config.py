class Config(object):
    DEBUG = False
    PORT = 5000
    DATABASE_NAME = "ransomware.sqlite"


class ProductionConfig(Config):
    DATABASE_NAME = "ranswomare_prod.sqlite"


class DevelopmentConfig(Config):
    DEBUG = True


class TestingConfig(Config):
    DEBUG = True


config = {
    "default": Config,
    "production": ProductionConfig,
    "development": DevelopmentConfig,
    "testing": TestingConfig

}
