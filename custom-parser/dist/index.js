"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const fs_1 = __importDefault(require("fs"));
const rawdata = fs_1.default.readFileSync('.\\..\\temp\\ble_data.json', 'utf-8');
const student = JSON.parse(rawdata.replace(/^\uFEFF/, ''));
console.log(JSON.stringify(student));
