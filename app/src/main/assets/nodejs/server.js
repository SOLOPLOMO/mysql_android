/*

      OPEN HTTP FOR FETCH DATA AND STRING

*/

/* Settings */
var host = "10.0.2.2" // | 10.0.2.2 Emulator | IPv4 PC

/***********/

var bodyParser = require('body-parser')
var express = require('express')
var app = express()
app.use(bodyParser.urlencoded({extended: true}))
app.use(bodyParser.json())
var argv = process.argv;
var port = argv[2]; // port
var mysql = require('mysql2'); // mysql2 is not depricated like mysql.
var server = app.listen(port);

app.post('/', (req, res) => {
    var query = req.body.query;
    var db = req.body.dbname;
    var con;
    if(db){
      con = mysql.createConnection({
              host: host,
              user: "root",
              password: "",
              database: db
            });
    }else{
       con = mysql.createConnection({
          host: host,
          user: "root",
          password: ""
        });
    }

    con.connect(function(err) {
        if (err) res.status(400).json({ error: "Αποτυχία Σύνδεσης στην ΒΔ" });
    });

    con.query(query, function (err, result) {
        if (err) res.status(400).json({ error: "Request Fail: " + err });
        res.send(JSON.stringify(result));
    });


   // Response has been Send and we Terminate the Node Process
})




