<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type, Authorization");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") { http_response_code(200); exit(); }

require_once "../config/database.php";
require_once "../models/Attendance.php";
require_once "../helpers/jwt.php";

$auth = JWT::getFromHeader();
if (!$auth) {
    http_response_code(401);
    echo json_encode(["error" => "Unauthorized"]);
    exit();
}

$db = (new Database())->getConnection();
$attendance = new Attendance($db);

$teacher_id = $auth["role"] === "teacher" ? $auth["sub"] : null;
$stats = $attendance->getStatistics($teacher_id);

echo json_encode($stats);