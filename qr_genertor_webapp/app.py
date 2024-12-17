from flask import Flask, render_template, request, flash
import pandas as pd
import qrcode
from io import BytesIO
import base64
import os
from werkzeug.utils import secure_filename

app = Flask(__name__)

UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'xlsx', 'xls'}

if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def validate_excel_structure(df):
    required_columns = ['Name', 'Id']
    missing_columns = [col for col in required_columns if col not in df.columns]
    return len(missing_columns) == 0, missing_columns

def generate_qr(data):
    try:
        qr = qrcode.QRCode(version=1, box_size=10, border=5)
        qr.add_data(data)
        qr.make(fit=True)
        img = qr.make_image(fill_color="black", back_color="white")
        buffered = BytesIO()
        img.save(buffered, format="PNG")
        img_str = base64.b64encode(buffered.getvalue()).decode()
        return img_str
    except Exception as e:
        print(f"Error generating QR code: {e}")
        return None

@app.route('/', methods=['GET', 'POST'])
def index():
    if request.method == 'POST':
        if 'file' not in request.files:
            return render_template('index.html', error="No file uploaded")
        
        file = request.files['file']
        if file.filename == '':
            return render_template('index.html', error="No file selected")
        
        if file and allowed_file(file.filename):
            try:
                # Read Excel file
                df = pd.read_excel(file)
                
                # Validate structure
                is_valid, missing_columns = validate_excel_structure(df)
                if not is_valid:
                    return render_template('index.html', error=f"Missing required columns: {', '.join(missing_columns)}")
                
                # Process data
                items = df.to_dict('records')
                for item in items:
                    # Create QR data from all available columns
                    qr_data = "\n".join([f"{k}: {v}" for k, v in item.items()])
                    item['qr_code'] = generate_qr(qr_data)
                
                return render_template('index.html', items=items, columns=df.columns)
            
            except Exception as e:
                return render_template('index.html', error=f"Error processing file: {str(e)}")
        
        return render_template('index.html', error="Invalid file type")
    
    return render_template('index.html')

if __name__ == '__main__':
    app.run(debug=True)