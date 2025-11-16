from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.firefox.options import Options as FirefoxOptions
from datetime import datetime


def get_logindata():
    with open('/app/logindata', 'r') as file:
        logindata = file.readlines()
        return [e.replace('\n', '') for e in logindata]

def get_login_id():
    return get_logindata()[0]


def get_password():
    return get_logindata()[1]


def set_login_id(driver):
    password = driver.find_element(By.NAME, "password")
    password.clear()
    password.send_keys(get_password())


def set_password(driver):
    login_id = driver.find_element(By.NAME, "loginid")
    login_id.clear()
    login_id.send_keys(get_login_id())


def submit(driver):
    submit_btn = driver.find_element(By.NAME, "action")
    submit_btn.click()


def auto_login():
    options = FirefoxOptions()
    driver = webdriver.Remote(options=options, command_executor="http://selenium:4444")
    driver.get("https://login.ruhr-uni-bochum.de/cgi-bin/start")
    set_login_id(driver)
    set_password(driver)
    submit(driver)
    html = driver.page_source
    if "Authentisierung gelungen" in html:
        print(datetime.now(), ": login successful")
    else:
        print(datetime.now(), ": login failed:", html)


if __name__ == '__main__':
    auto_login()
