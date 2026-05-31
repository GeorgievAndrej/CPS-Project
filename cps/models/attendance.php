<?php
class Attendance {
    private $conn;
    private $table = "attendance_records";

    public function __construct($db) {
        $this->conn = $db;
    }

    public function bulkInsert($records, $teacher_id) {
        $saved = 0;
        $duplicates = 0;
        $errors = 0;

        foreach ($records as $rec) {
            try {
                // Duplicate check
                $check = $this->conn->prepare(
                    "SELECT id FROM " . $this->table . "
                     WHERE student_external_id = ? AND course_id = ?
                     AND DATE(tapped_at) = DATE(?)"
                );
                $check->execute([
                    $rec["student_external_id"],
                    $rec["course_id"],
                    $rec["tapped_at"]
                ]);
                $exists = $check->fetch();

                $stmt = $this->conn->prepare(
                    "INSERT INTO " . $this->table . "
                     (student_external_id, student_name, course_id, teacher_id, tapped_at, session_id, is_duplicate)
                     VALUES (?, ?, ?, ?, ?, ?, ?)"
                );
                $stmt->execute([
                    $rec["student_external_id"],
                    $rec["student_name"],
                    $rec["course_id"],
                    $teacher_id,
                    $rec["tapped_at"],
                    $rec["session_id"] ?? null,
                    $exists ? 1 : 0
                ]);

                if ($exists) $duplicates++;
                else $saved++;

            } catch (Exception $e) {
                $errors++;
            }
        }

        return ["saved" => $saved, "duplicates" => $duplicates, "errors" => $errors];
    }

    public function getAll($filters = []) {
        $where = ["is_duplicate = 0"];
        $params = [];

        if (!empty($filters["course_id"])) {
            $where[] = "a.course_id = ?";
            $params[] = $filters["course_id"];
        }
        if (!empty($filters["student_name"])) {
            $where[] = "a.student_name LIKE ?";
            $params[] = "%" . $filters["student_name"] . "%";
        }
        if (!empty($filters["date_from"])) {
            $where[] = "DATE(a.tapped_at) >= ?";
            $params[] = $filters["date_from"];
        }
        if (!empty($filters["date_to"])) {
            $where[] = "DATE(a.tapped_at) <= ?";
            $params[] = $filters["date_to"];
        }

        $whereStr = implode(" AND ", $where);

        $query = "SELECT a.*, c.name as course_name, c.code as course_code
                  FROM " . $this->table . " a
                  LEFT JOIN courses c ON a.course_id = c.id
                  WHERE $whereStr
                  ORDER BY a.tapped_at DESC";

        $stmt = $this->conn->prepare($query);
        $stmt->execute($params);
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function getStatistics($teacher_id = null) {
        $params = [];
        $where = "WHERE a.is_duplicate = 0";

        if ($teacher_id) {
            $where .= " AND a.teacher_id = ?";
            $params[] = $teacher_id;
        }

        // Per course
        $stmt = $this->conn->prepare(
            "SELECT c.id, c.name, c.code,
                    COUNT(a.id) as total_records,
                    COUNT(DISTINCT a.student_external_id) as unique_students,
                    COUNT(DISTINCT a.session_id) as total_sessions
             FROM attendance_records a
             LEFT JOIN courses c ON a.course_id = c.id
             $where
             GROUP BY c.id, c.name, c.code"
        );
        $stmt->execute($params);
        $courseStats = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // Daily trend (last 30 days)
        $stmt2 = $this->conn->prepare(
            "SELECT DATE(a.tapped_at) as date, COUNT(*) as count
             FROM attendance_records a
             $where
             AND a.tapped_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
             GROUP BY DATE(a.tapped_at)
             ORDER BY date ASC"
        );
        $stmt2->execute($params);
        $dailyTrend = $stmt2->fetchAll(PDO::FETCH_ASSOC);

        return ["course_stats" => $courseStats, "daily_trend" => $dailyTrend];
    }
}