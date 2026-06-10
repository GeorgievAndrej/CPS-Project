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

$db         = (new Database())->getConnection();
$attendance = new Attendance($db);

if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $raw  = file_get_contents("php://input");
    $data = json_decode($raw, true);

    if (!$data) {
        http_response_code(400);
        echo json_encode(["error" => "Invalid JSON"]);
        exit();
    }

    /*
     * ЗОШТО двојна проверка?
     * Android app испраќа: { "records": [ {...} ] }  (SyncRequestBody)
     * Некои клиенти може директно да испратат: [ {...} ] или { ...single... }
     * Поддржуваме и трите формати за robustness.
     */
    if (isset($data["records"]) && is_array($data["records"])) {
        $records = $data["records"];
    } elseif (isset($data[0])) {
        $records = $data;
    } else {
        $records = [$data];
    }

    if (empty($records)) {
        http_response_code(400);
        echo json_encode(["error" => "No records provided"]);
        exit();
    }

    $result = $attendance->bulkInsert($records, $auth["sub"]);
    http_response_code(201);
    echo json_encode(array_merge(
        ["received" => count($records)],
        $result
    ));

} else {
    // GET — врати ги записите со филтри
    $filters = [
        "course_id"    => $_GET["course_id"]    ?? null,
        "student_name" => $_GET["student_name"] ?? null,
        "date_from"    => $_GET["date_from"]    ?? null,
        "date_to"      => $_GET["date_to"]      ?? null,
    ];

    // Role-based: наставникот гледа само свои записи
    if ($auth["role"] === "teacher") {
        $filters["teacher_id"] = $auth["sub"];
    }

    $records = $attendance->getAll($filters);
    echo json_encode(["total" => count($records), "items" => $records]);
}
