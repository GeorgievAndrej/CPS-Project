<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Authorization");


if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") { http_response_code(200); exit(); }

require_once "../config/database.php";
require_once "../models/User.php";
require_once "../helpers/jwt.php";

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data["username"]) || empty($data["password"])) {
    http_response_code(400);
    echo json_encode(["error" => "Username and password required"]);
    exit();
}

$db = (new Database())->getConnection();
$user = (new User($db))->findByUsername($data["username"]);

if (!$user || !password_verify($data["password"], $user["password"])) {
    http_response_code(401);
    echo json_encode(["error" => "Incorrect username or password"]);
    exit();
}

$token = JWT::generate([
    "sub" => $user["id"],
    "role" => $user["role"],
    "username" => $user["username"]
]);

echo json_encode([
    "access_token" => $token,
    "token_type" => "bearer",
    "user" => [
        "id" => $user["id"],
        "username" => $user["username"],
        "full_name" => $user["full_name"],
        "role" => $user["role"],
        "email" => $user["email"]
    ]
]);
exit();