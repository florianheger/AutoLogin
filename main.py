from selenium import webdriver
from selenium.webdriver.common.by import By


def get_login_id():
    return ""


def get_password():
    return ""


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
    driver = webdriver.Firefox()
    driver.get("https://login.ruhr-uni-bochum.de/cgi-bin/start")
    set_login_id(driver)
    set_password(driver)
    submit(driver)


if __name__ == '__main__':
    auto_login()
