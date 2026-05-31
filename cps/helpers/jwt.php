<?php
class JWT {
    private static $secret = "cps-secret-key-2024-change-in-production";

    public static function generate($data) {
        $header = base64_encode(json_encode(["alg" => "HS256", "typ" => "JWT"]));
        $payload = base64_encode(json_encode(array_merge($data, [
            "exp" => time() + 3600
        ])));
        $signature = base64_encode(hash_hmac("sha256", "$header.$payload", self::$secret, true));
        return "$header.$payload.$signature";
    }

    public static function verify($token) {
        $parts = explode(".", $token);
        if (count($parts) !== 3) return null;

        list($header, $payload, $signature) = $parts;
        $validSig = base64_encode(hash_hmac("sha256", "$header.$payload", self::$secret, true));

        if ($signature !== $validSig) return null;

        $data = json_decode(base64_decode($payload), true);
        if ($data["exp"] < time()) return null;

        return $data;
    }

    public static function getFromHeader() {
        $headers = getallheaders();
        if (!isset($headers["Authorization"])) return null;
        $parts = explode(" ", $headers["Authorization"]);
        if (count($parts) !== 2 || $parts[0] !== "Bearer") return null;
        return self::verify($parts[1]);
    }
}