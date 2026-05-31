<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");
header("Access-Control-Allow-Methods: GET, POST");
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

if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $data = json_decode(file_get_contents("php://input"), true);

    if (empty($data["records"])) {
        http_response_code(400);
        echo json_encode(["error" => "No records provided"]);
        exit();
    }

    $result = $attendance->bulkInsert($data["records"], $auth["sub"]);
    http_response_code(201);
    echo json_encode(array_merge(
        ["received" => count($data["records"])],
        $result
    ));

} else {
    $filters = [
        "course_id" => $_GET["course_id"] ?? null,
        "student_name" => $_GET["student_name"] ?? null,
        "date_from" => $_GET["date_from"] ?? null,
        "date_to" => $_GET["date_to"] ?? null,
    ];

    // Role-based: teacher sees only their data
    if ($auth["role"] === "teacher") {
        $filters["teacher_id"] = $auth["sub"];
    }

    $records = $attendance->getAll($filters);
    echo json_encode(["total" => count($records), "items" => $records]);
}