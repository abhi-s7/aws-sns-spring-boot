// Load topics for selection
async function loadTopics() {
    const topicSelectListDiv = document.getElementById('topicSelectList');

    try {
        const response = await axios.get('/api/sns/list-topics');
        
        if (response.data.length === 0) {
            topicSelectListDiv.innerHTML = '<div class="empty-state">No topics available</div>';
        } else {
            topicSelectListDiv.innerHTML = response.data.map(arn => 
                `<div class="queue-item" style="cursor: pointer;" onclick="selectTopic('${arn}')">${arn}</div>`
            ).join('');
        }
    } catch (error) {
        topicSelectListDiv.innerHTML = '<div class="error">Failed to load topics</div>';
    }
}

// Select topic from list
function selectTopic(topicArn) {
    document.getElementById('selectedTopicArn').value = topicArn;
}

// Send notification
async function sendNotification(event) {
    event.preventDefault();
    const topicArn = document.getElementById('selectedTopicArn').value;
    const subject = document.getElementById('subject').value;
    const userName = document.getElementById('userName').value;
    const giftCardType = document.getElementById('giftCardType').value;
    const amount = parseFloat(document.getElementById('amount').value);
    const date = document.getElementById('date').value;
    const resultDiv = document.getElementById('notificationResult');

    if (!topicArn) {
        resultDiv.className = 'result error';
        resultDiv.textContent = 'Please select a topic ARN first';
        return;
    }

    try {
        const response = await axios.post('/api/sns/publish', {
            topicArn: topicArn,
            subject: subject,
            giftCard: {
                userName: userName,
                giftCardType: giftCardType,
                amount: amount,
                date: date
            }
        });
        
        resultDiv.className = 'result';
        resultDiv.textContent = `${response.data.message} (ID: ${response.data.messageId})`;
        document.getElementById('notificationForm').reset();
        
        // Reset default subject and date
        document.getElementById('subject').value = 'Gift Card Delivery Notification';
        document.getElementById('date').valueAsDate = new Date();
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.textContent = error.response?.data || 'Failed to send notification';
    }
}

// Set defaults on page load
window.onload = () => {
    document.getElementById('date').valueAsDate = new Date();
    loadTopics();
};
