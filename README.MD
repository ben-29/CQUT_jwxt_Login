#重庆理工大学 教务系统 模拟登录 Java
重庆理工大学 教务系统 http://jwxt.i.cqut.edu.cn

* 功能 - 使用 HttpComponents 模拟登录教务系统
* 过程 目标:http://i.cqut.edu.cn/zfca/login?yhlx=student&login=0122579031373493728&url=xs_main.aspx
    *  GET 获取cookie & lt
        * cookie <== Header:set-Cookie
        * lt <== html
    *  POST 附带参数
        *  cookie
        *  lt
        *  username=学号
        *  password=密码
        *  _evenId=submit
    * 成功 登录链接: /(b5nyljrkw4i0lmqvfrnyvn45)/xs_main.aspx?xh=`学号`&type=1