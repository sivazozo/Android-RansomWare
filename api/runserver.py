import os

from ransomware import create_app

if __name__ == '__main__':
    mode = 'default'
    if os.environ.get('MODE'):
        mode = os.environ.get('MODE')

    app = create_app(mode)
    app.run()
