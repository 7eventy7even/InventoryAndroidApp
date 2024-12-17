# **Excel to QR Code Generator**

This Flask app converts entries from an Excel sheet into QR codes. The Excel file must include two required columns: **Name** and **Id**.

---

## **Setup and Run the App**

Follow the steps below to set up and run the application:

### **1. Clone the Repository**
Run the following command to clone the repository:
git clone <repository-url>
cd <repository-folder>

### **2. Create and Activate a Virtual Environment
Create the virtual environment:

python -m venv venv

**Activate the virtual environment:**
**Windows**:

.\venv\Scripts\activate

**Mac/Linux**:

source venv/bin/activate

### **3. Install Required Packages

Install the dependencies listed in requirements.txt:

pip install -r requirements.txt

### **4. Run the Flask Application

Start the app with the following command:

python app.py

### **5. Access the App
Open your browser and go to:

http://127.0.0.1:5000
