from flask import Flask, jsonify, request
from zxcvbn import zxcvbn

app = Flask(__name__)

@app.get('/health')
def health():
    return jsonify({'status': 'ok'})

@app.post('/validate')
def validate_password():
    payload = request.get_json(silent=True) or {}
    password = payload.get('password', '')
    result = zxcvbn(password)
    return jsonify({
        'score': result['score'],
        'feedback': result.get('feedback', {}).get('suggestions', ['Aucun feedback disponible'])[0],
        'crack_time_display': result.get('crack_times_display', {}).get('offline_fast_hashing_1e10_per_second', 'N/A'),
        'crack_time_seconds': result.get('crack_times_seconds', {}).get('offline_fast_hashing_1e10_per_second', -1),
        'guesses': result.get('guesses', -1),
        'password_length': len(password)
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
