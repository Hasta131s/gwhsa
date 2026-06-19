<?xml version="1.0" encoding="utf-8"?>
<!-- PHP Upload Helper Code for Ödev Pro -->
<?php
header('Content-Type: application/json; charset=utf-8');

$target_dir = "uploads/";
if (!file_exists($target_dir)) {
    mkdir($target_dir, 0777, true);
}

$response = array();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (isset($_FILES['image'])) {
        $file_name = basename($_FILES["image"]["name"]);
        $target_file = $target_dir . time() . "_" . preg_replace("/[^a-zA-Z0-9.]/", "_", $file_name);
        $imageFileType = strtolower(pathinfo($target_file, PATHINFO_EXTENSION));

        // Check if image file is an actual image
        $check = getimagesize($_FILES["image"]["tmp_name"]);
        if ($check !== false) {
            if (move_uploaded_file($_FILES["image"]["tmp_name"], $target_file)) {
                $actual_link = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http") . "://$_SERVER[HTTP_HOST]" . dirname($_SERVER['REQUEST_URI']) . "/" . $target_file;
                $response["success"] = true;
                $response["message"] = "Görsel başarıyla yüklendi.";
                $response["image_url"] = $actual_link;
                
                // OCR ve Yapay Zeka analiz simülasyonu (Optional direct connection to Gemini, or we can reply with sample OCR if needed)
                $response["ocr_text"] = "Soru:\nOtoparkta 205 tane kırmızı araba vardır. Kırmızı arabalardan 36 fazla beyaz araba vardır. Daha sonra otoparka 75 beyaz araba daha geldiğine göre, otoparkta toplam kaç beyaz araba olmuştur?\n\nA) 245\nB) 316\nC) 521";
            } else {
                $response["success"] = false;
                $response["message"] = "Dosya yüklenirken bir hata oluştu.";
            }
        } else {
            $response["success"] = false;
            $response["message"] = "Yüklenen dosya geçerli bir görsel değil.";
        }
    } else {
        $response["success"] = false;
        $response["message"] = "Yüklenecek görsel ('image' parametresi) bulunamadı.";
    }
} else {
    $response["success"] = false;
    $response["message"] = "Yalnızca POST istekleri desteklenmektedir.";
}

echo json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT);
?>
