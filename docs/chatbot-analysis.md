# Ghi chu nang cap chatbot QLVT

## Hien trang truoc khi nang cap

- `ChatbotController` nhan `/api/chatbot`, goi `ChatbotService` va luu lich su qua `ChatSession`/`ChatMessage`.
- `ChatbotService` xu ly bang rule intent va tra text thuan; mot so cau hoi ton kho/vi tri/lo da truy van du lieu that nhung chua co response cau truc cho UI.
- `MaterialSearchService` da normalize tieng Viet va fuzzy match co ban, nhung chua co scoring ro rang, alias y te day du, tach nhieu vat tu, hoac co che hoi lai khi ten vat tu mo ho.

## Du lieu nghiep vu chatbot dang dung

- Vat tu: `Material`, truong chinh gom `code`, `name`, `aliasText`, `unit`, `minStock`, `actualQuantity`, `reservedQuantity`, `pendingIssueQuantity`.
- Ton kho theo kho/ke/lo: `StockBalance`, lien ket `Material`, `MaterialBatch`, `Warehouse`, `StorageLocation`.
- Lo, han dung, ngay nhap: `MaterialBatch`, gom `batchNumber`, `receiptDate`, `expiryDate`, `status`, `quantity`.
- Ton khoa/phong: `DepartmentStock`, gom `department`, `material`, `batch`, `quantityOnHand`, `lastReceivedAt`.
- Chat history: tiep tuc dung `ChatSession` va `ChatMessage`; khong them bang/cot moi.

## Huong nang cap

- Them `ChatbotNlpService` de nhan dien intent nghiep vu bang tieng Viet co dau/khong dau, alias, thoi han het han va pham vi kho/khoa.
- Nang `MaterialSearchService` bang alias y te, token scoring va fuzzy match nhe.
- Nang `ChatbotService` de tra loi tu du lieu that: tong ton, kho, ke, lo, ngay nhap, han dung, trang thai, goi y FEFO/FIFO.
- API moi `/api/chatbot/message` tra response co `success`, `intent`, `message`, `items`, `suggestions`; endpoint cu `/api/chatbot` van giu tuong thich.
