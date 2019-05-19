#!/usr/bin/env node

const readline = require("readline");

const rl = readline.createInterface({
    input: process.stdin,
    terminal: false
});
rl.on('line', function (actual) {
    const expected = "standard input";
    if (actual != expected) {
        process.exit(5)
    } else {
        console.error("standard error");
        console.log("standard output");
        rl.close();
    }
});

